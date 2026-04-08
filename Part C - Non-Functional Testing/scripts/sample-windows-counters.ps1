param(
    [Parameter(Mandatory = $true)]
    [string]$OutputPath,

    [int]$IntervalMilliseconds = 50
)

$ErrorActionPreference = "Stop"
$culture = [System.Globalization.CultureInfo]::InvariantCulture

$outputDirectory = Split-Path -Path $OutputPath -Parent
if ($outputDirectory) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

"timestampUtc,cpuPercent,availableMemoryMB" | Set-Content -Path $OutputPath -Encoding UTF8

while ($true) {
    $snapshot = Get-Counter -Counter "\Processor(_Total)\% Processor Time", "\Memory\Available MBytes"
    $cpuSample = ($snapshot.CounterSamples | Where-Object { $_.Path -like "*Processor(_Total)*" } | Select-Object -First 1).CookedValue
    $memorySample = ($snapshot.CounterSamples | Where-Object { $_.Path -like "*Memory*Available MBytes*" } | Select-Object -First 1).CookedValue
    $timestamp = [DateTime]::UtcNow.ToString("o", $culture)
    $cpuText = ([double]$cpuSample).ToString("F3", $culture)
    $memoryText = ([double]$memorySample).ToString("F3", $culture)
    $line = "$timestamp,$cpuText,$memoryText"
    Add-Content -Path $OutputPath -Value $line -Encoding UTF8
    Start-Sleep -Milliseconds $IntervalMilliseconds
}
