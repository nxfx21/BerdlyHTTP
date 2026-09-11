#!/usr/bin/env pwsh
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

# Ensure compatible JDK for Gradle 8.8 (supports up to Java 21)
if ($IsMacOS) {
    $javaVer = (java -version 2>&1 | Select-String '"(\d+)' | ForEach-Object { [int]$_.Matches[0].Groups[1].Value })
    if ($javaVer -gt 21 -or -not $env:JAVA_HOME) {
        $jdk = & /usr/libexec/java_home -v 21 2>$null
        if (-not $jdk) { $jdk = & /usr/libexec/java_home -v 17 2>$null }
        if ($jdk) { $env:JAVA_HOME = $jdk.Trim() }
    }
}

Write-Host "Building BerdlyHTTP..." -ForegroundColor Cyan

if ($IsWindows) {
    & .\gradlew.bat build
} else {
    sh ./gradlew build
}

if ($LASTEXITCODE -eq 0) {
    $jar = Get-ChildItem -Path "build/libs" -Filter "berdlyHttp-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jar) {
        Write-Host "Build successful: $($jar.FullName)" -ForegroundColor Green
    }
} else {
    exit $LASTEXITCODE
}
