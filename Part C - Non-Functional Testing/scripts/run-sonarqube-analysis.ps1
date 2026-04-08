param(
    [Parameter(Mandatory = $false)]
    [string]$SonarHostUrl = $env:SONAR_HOST_URL,

    [Parameter(Mandatory = $false)]
    [string]$SonarToken = $env:SONAR_TOKEN,

    [string]$ScannerPath = "sonar-scanner",

    [string]$MavenRepoLocal,

    [string]$ThingifierDependencyJar = "C:\Users\Admin\OneDrive - McGill University\McGill\WIN 2026\ECSE 429\Project\Project documents\Application_Being_Tested\runTodoManagerRestAPI-1.5.5.jar",

    [string]$ThingifierSourceZip = "C:\Users\Admin\OneDrive - McGill University\McGill\WIN 2026\ECSE 429\Project\Project documents\Application_Being_Tested\thingifier-1.5.5.zip",

    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"

$partCRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$challengerDir = Join-Path $partCRoot "thingifier-1.5.5\challenger"
$challengerPom = Join-Path $challengerDir "pom.xml"
$sonarProperties = Join-Path $partCRoot "sonar-project.properties"
$challengeSourceRoot = Join-Path $challengerDir "src\main\java\uk\co\compendiumdev\challenge"

if ([string]::IsNullOrWhiteSpace($MavenRepoLocal)) {
    $MavenRepoLocal = Join-Path $partCRoot ".m2"
}

if ([string]::IsNullOrWhiteSpace($SonarHostUrl)) {
    throw "SonarHostUrl was not provided. Pass -SonarHostUrl or set SONAR_HOST_URL."
}

if ([string]::IsNullOrWhiteSpace($SonarToken)) {
    throw "SonarToken was not provided. Pass -SonarToken or set SONAR_TOKEN."
}

if (-not (Test-Path $challengerPom)) {
    throw "Could not find challenger module pom at $challengerPom"
}

if (-not (Test-Path $sonarProperties)) {
    throw "Could not find sonar-project.properties at $sonarProperties"
}

New-Item -ItemType Directory -Path $MavenRepoLocal -Force | Out-Null

function Install-ThingifierDependency {
    param(
        [string]$JarPath,
        [string]$LocalRepo
    )

    if (-not (Test-Path $JarPath)) {
        throw "Fallback dependency jar was not found: $JarPath"
    }

    $artifactDirectory = Join-Path $LocalRepo "uk\co\compendiumdev\thingifier\1.5.5"
    New-Item -ItemType Directory -Path $artifactDirectory -Force | Out-Null

    $artifactJar = Join-Path $artifactDirectory "thingifier-1.5.5.jar"
    $artifactPom = Join-Path $artifactDirectory "thingifier-1.5.5.pom"

    Copy-Item -Path $JarPath -Destination $artifactJar -Force

    @"
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>uk.co.compendiumdev</groupId>
  <artifactId>thingifier</artifactId>
  <version>1.5.5</version>
  <packaging>jar</packaging>
</project>
"@ | Set-Content -Path $artifactPom -Encoding UTF8

    if (-not (Test-Path $artifactJar) -or -not (Test-Path $artifactPom)) {
        throw "Failed to place fallback thingifier dependency into $artifactDirectory"
    }
}

function Restore-MissingChallengeSource {
    param(
        [string]$ZipPath,
        [string]$DestinationRoot
    )

    $markerFile = Join-Path $DestinationRoot "challenges\ChallengeDefinitions.java"
    if (Test-Path $markerFile) {
        return
    }

    if (-not (Test-Path $ZipPath)) {
        throw "The copied source tree is incomplete and the source zip was not found: $ZipPath"
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($ZipPath)

    try {
        foreach ($entry in $archive.Entries) {
            if (-not $entry.FullName.StartsWith("thingifier-1.5.5/challenger/src/main/java/uk/co/compendiumdev/challenge/")) {
                continue
            }

            $relativePath = $entry.FullName.Substring("thingifier-1.5.5/challenger/src/main/java/uk/co/compendiumdev/challenge/".Length)
            if ([string]::IsNullOrWhiteSpace($relativePath)) {
                continue
            }

            $targetPath = Join-Path $DestinationRoot $relativePath.Replace("/", "\")

            if ($entry.FullName.EndsWith("/")) {
                New-Item -ItemType Directory -Path $targetPath -Force | Out-Null
                continue
            }

            $targetDirectory = Split-Path -Path $targetPath -Parent
            if ($targetDirectory) {
                New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null
            }

            $inputStream = $entry.Open()
            try {
                $outputStream = [System.IO.File]::Create($targetPath)
                try {
                    $inputStream.CopyTo($outputStream)
                } finally {
                    $outputStream.Dispose()
                }
            } finally {
                $inputStream.Dispose()
            }
        }
    } finally {
        $archive.Dispose()
    }
}

if (-not $SkipCompile) {
    Restore-MissingChallengeSource -ZipPath $ThingifierSourceZip -DestinationRoot $challengeSourceRoot

    Push-Location $challengerDir
    try {
        & mvn "-f" $challengerPom "-Dmaven.repo.local=$MavenRepoLocal" "-DskipTests" "compile"
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Initial compile failed. Installing fallback thingifier dependency from local jar and retrying."
            Install-ThingifierDependency -JarPath $ThingifierDependencyJar -LocalRepo $MavenRepoLocal
            & mvn "-f" $challengerPom "-Dmaven.repo.local=$MavenRepoLocal" "-DskipTests" "compile"
            if ($LASTEXITCODE -ne 0) {
                throw "Maven compile failed for the challenger module even after installing the fallback dependency."
            }
        }
    } finally {
        Pop-Location
    }
}

Push-Location $partCRoot
try {
    & $ScannerPath `
        "-Dproject.settings=$sonarProperties" `
        "-Dsonar.host.url=$SonarHostUrl" `
        "-Dsonar.login=$SonarToken" `
        "-Dsonar.token=$SonarToken"

    if ($LASTEXITCODE -ne 0) {
        throw "sonar-scanner execution failed."
    }
} finally {
    Pop-Location
}

Write-Host "SonarQube analysis completed for $challengerDir"
