param(
    [string]$JarPath = "C:\Users\Admin\OneDrive - McGill University\McGill\WIN 2026\ECSE 429\Project\Project documents\Application_Being_Tested\runTodoManagerRestAPI-1.5.5.jar",
    [string]$BaseUrl = "http://localhost:4567",
    [int[]]$StartingCounts = @(100, 250, 500, 1000, 2000),
    [int]$WarmupIterations = 20,
    [int]$MeasuredIterations = 100,
    [int]$SampleIntervalSeconds = 0,
    [int]$SampleIntervalMilliseconds = 50,
    [int]$SamplerReadyTimeoutSeconds = 10,
    [string]$JavaCommand = "java",
    [string]$JavacCommand = "javac"
)

$ErrorActionPreference = "Stop"

$partCRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$performanceSuiteDir = Join-Path $partCRoot "performance-suite"
$performanceTargetDir = Join-Path $performanceSuiteDir "target"
$performanceClassesDir = Join-Path $performanceTargetDir "classes"
$scriptsDir = Join-Path $partCRoot "scripts"
$resultsRoot = Join-Path $partCRoot "results"
$transactionsDir = Join-Path $resultsRoot "transactions"
$systemMetricsDir = Join-Path $resultsRoot "system-metrics"
$summaryDir = Join-Path $resultsRoot "summary"
$mavenRepoLocal = Join-Path $partCRoot ".m2"
$sampleScript = Join-Path $scriptsDir "sample-windows-counters.ps1"
$summaryCsvPath = Join-Path $summaryDir "performance-summary.csv"

function Ensure-Directory {
    param([string]$Path)
    New-Item -ItemType Directory -Path $Path -Force | Out-Null
}

function Stop-ManagedProcess {
    param([System.Diagnostics.Process]$Process)

    if ($null -ne $Process -and -not $Process.HasExited) {
        Stop-Process -Id $Process.Id -Force
        $Process.WaitForExit()
    }
}

function Wait-ForApi {
    param(
        [string]$HealthUrl,
        [int]$TimeoutSeconds = 60
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $HealthUrl -TimeoutSec 5 -UseBasicParsing
            if ($response.StatusCode -eq 200) {
                return
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }

    throw "Timed out waiting for API endpoint $HealthUrl"
}

function Get-DataRowCount {
    param([string]$CsvPath)

    if (-not (Test-Path $CsvPath)) {
        return 0
    }

    try {
        $lines = Get-Content -Path $CsvPath -ErrorAction Stop
        if ($null -eq $lines) {
            return 0
        }

        return [Math]::Max($lines.Count - 1, 0)
    } catch [System.IO.IOException] {
        return 0
    } catch [System.UnauthorizedAccessException] {
        return 0
    }
}

function Wait-ForMetricSamplerReady {
    param(
        [string]$CsvPath,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ((Get-DataRowCount -CsvPath $CsvPath) -ge 1) {
            return
        }

        Start-Sleep -Milliseconds 50
    }

    throw "Metric sampler did not record any samples within $TimeoutSeconds seconds: $CsvPath"
}

function Build-PerformanceSuite {
    Ensure-Directory -Path $mavenRepoLocal
    Ensure-Directory -Path $performanceTargetDir

    $runnerJar = Join-Path $performanceTargetDir "performance-suite-1.0-SNAPSHOT.jar"
    $mavenBuildSucceeded = $false

    Push-Location $performanceSuiteDir
    try {
        & mvn "-Dmaven.repo.local=$mavenRepoLocal" "-DskipTests" "package"
        if ($LASTEXITCODE -eq 0 -and (Test-Path $runnerJar)) {
            $mavenBuildSucceeded = $true
        }
    } catch {
        $mavenBuildSucceeded = $false
    } finally {
        Pop-Location
    }

    if ($mavenBuildSucceeded) {
        return [pscustomobject]@{
            Type = "jar"
            Path = $runnerJar
        }
    }

    Write-Warning "Maven build failed. Falling back to direct javac compilation for the performance harness."

    if (Test-Path $performanceClassesDir) {
        Remove-Item -Path $performanceClassesDir -Recurse -Force
    }
    Ensure-Directory -Path $performanceClassesDir

    $javaFiles = Get-ChildItem -Path (Join-Path $performanceSuiteDir "src\main\java") -Recurse -Filter *.java |
        Select-Object -ExpandProperty FullName

    if (-not $javaFiles) {
        throw "No Java source files were found in $performanceSuiteDir"
    }

    & $JavacCommand "-d" $performanceClassesDir $javaFiles
    if ($LASTEXITCODE -ne 0) {
        throw "Direct javac compilation failed for the performance harness."
    }

    return [pscustomobject]@{
        Type = "classes"
        Path = $performanceClassesDir
    }
}

function Invoke-Harness {
    param(
        $Runner,
        [string]$Operation,
        [int]$StartingCount,
        [string]$TransactionCsv,
        [string]$LatencySummaryCsv
    )

    $commonArgs = @(
        "--operation=$Operation",
        "--starting-count=$StartingCount",
        "--warmup=$WarmupIterations",
        "--measured=$MeasuredIterations",
        "--base-url=$BaseUrl",
        "--transaction-csv=$TransactionCsv",
        "--summary-csv=$LatencySummaryCsv"
    )

    if ($Runner.Type -eq "jar") {
        & $JavaCommand "-jar" $Runner.Path @commonArgs
    } else {
        & $JavaCommand "-cp" $Runner.Path "ecse429.perf.TodoPerformanceRunner" @commonArgs
    }

    if ($LASTEXITCODE -ne 0) {
        throw "Performance harness failed for operation '$Operation' and count '$StartingCount'."
    }
}

function Get-MetricAverage {
    param([double[]]$Values)

    if (-not $Values -or $Values.Count -eq 0) {
        return ""
    }

    return [math]::Round((($Values | Measure-Object -Average).Average), 3)
}

function Get-MetricMinimum {
    param([double[]]$Values)

    if (-not $Values -or $Values.Count -eq 0) {
        return ""
    }

    return [math]::Round((($Values | Measure-Object -Minimum).Minimum), 3)
}

function Get-SystemMetricSummary {
    param([string]$MetricsCsv)

    if (-not (Test-Path $MetricsCsv)) {
        throw "System metrics file was not created: $MetricsCsv"
    }

    $rows = @(Import-Csv -Path $MetricsCsv)
    if ($rows.Count -eq 0) {
        throw "No system metric samples were collected for $MetricsCsv"
    }

    $cpuValues = @($rows | ForEach-Object { [double]$_.cpuPercent })
    $memoryValues = @($rows | ForEach-Object { [double]$_.availableMemoryMB })

    return [pscustomobject]@{
        SampleCount = $rows.Count
        AverageCpuPercent = Get-MetricAverage -Values $cpuValues
        AverageAvailableMemoryMB = Get-MetricAverage -Values $memoryValues
        MinimumAvailableMemoryMB = Get-MetricMinimum -Values $memoryValues
    }
}

function Append-CombinedSummary {
    param(
        [string]$LatencySummaryCsv,
        [string]$MetricsCsv,
        [string]$TransactionCsv,
        [string]$MasterSummaryCsv
    )

    $latencySummary = Import-Csv -Path $LatencySummaryCsv | Select-Object -First 1
    if ($null -eq $latencySummary) {
        throw "Latency summary file was empty: $LatencySummaryCsv"
    }

    $systemSummary = Get-SystemMetricSummary -MetricsCsv $MetricsCsv

    $combined = [pscustomobject]@{
        startingObjectCount = $latencySummary.startingObjectCount
        operation = $latencySummary.operation
        measuredIterations = $latencySummary.measuredIterations
        meanLatencyMs = $latencySummary.meanLatencyMs
        medianLatencyMs = $latencySummary.medianLatencyMs
        p95LatencyMs = $latencySummary.p95LatencyMs
        averageCpuPercent = $systemSummary.AverageCpuPercent
        averageAvailableMemoryMB = $systemSummary.AverageAvailableMemoryMB
        minimumAvailableMemoryMB = $systemSummary.MinimumAvailableMemoryMB
        sampleCount = $systemSummary.SampleCount
        transactionCsv = [IO.Path]::GetFileName($TransactionCsv)
        systemMetricsCsv = [IO.Path]::GetFileName($MetricsCsv)
    }

    $exportParams = @{
        Path = $MasterSummaryCsv
        NoTypeInformation = $true
    }

    if (Test-Path $MasterSummaryCsv) {
        $exportParams.Append = $true
    }

    $combined | Export-Csv @exportParams
}

Ensure-Directory -Path $transactionsDir
Ensure-Directory -Path $systemMetricsDir
Ensure-Directory -Path $summaryDir

if (-not (Test-Path $JarPath)) {
    throw "Todo Manager jar was not found: $JarPath"
}

$effectiveSampleIntervalMilliseconds = if ($SampleIntervalMilliseconds -gt 0) {
    $SampleIntervalMilliseconds
} elseif ($SampleIntervalSeconds -gt 0) {
    $SampleIntervalSeconds * 1000
} else {
    50
}

$runner = Build-PerformanceSuite

if (Test-Path $summaryCsvPath) {
    Remove-Item -Path $summaryCsvPath -Force
}

$operations = @("create", "update", "delete")

foreach ($operation in $operations) {
    foreach ($startingCount in $StartingCounts) {
        $runLabel = "{0}_{1}_{2}" -f $operation, $startingCount, (Get-Date -Format "yyyyMMdd_HHmmss")
        $transactionCsv = Join-Path $transactionsDir "$runLabel-transactions.csv"
        $latencySummaryCsv = Join-Path $summaryDir "$runLabel-latency.csv"
        $systemMetricsCsv = Join-Path $systemMetricsDir "$runLabel-system.csv"

        $apiProcess = $null
        $metricsProcess = $null

        try {
            $apiProcess = Start-Process `
                -FilePath $JavaCommand `
                -ArgumentList @("-jar", "`"$JarPath`"") `
                -PassThru `
                -WindowStyle Hidden
            Wait-ForApi -HealthUrl "$BaseUrl/todos"

            $metricsProcess = Start-Process `
                -FilePath "powershell" `
                -ArgumentList @(
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    "`"$sampleScript`"",
                    "-OutputPath",
                    "`"$systemMetricsCsv`"",
                    "-IntervalMilliseconds",
                    $effectiveSampleIntervalMilliseconds
                ) `
                -PassThru `
                -WindowStyle Hidden

            Wait-ForMetricSamplerReady -CsvPath $systemMetricsCsv -TimeoutSeconds $SamplerReadyTimeoutSeconds

            Invoke-Harness `
                -Runner $runner `
                -Operation $operation `
                -StartingCount $startingCount `
                -TransactionCsv $transactionCsv `
                -LatencySummaryCsv $latencySummaryCsv

            Stop-ManagedProcess -Process $metricsProcess
            $metricsProcess = $null

            Append-CombinedSummary `
                -LatencySummaryCsv $latencySummaryCsv `
                -MetricsCsv $systemMetricsCsv `
                -TransactionCsv $transactionCsv `
                -MasterSummaryCsv $summaryCsvPath
        } finally {
            Stop-ManagedProcess -Process $metricsProcess
            Stop-ManagedProcess -Process $apiProcess
            Start-Sleep -Seconds 2
        }
    }
}

Write-Host "Performance test suite finished. Master summary: $summaryCsvPath"
