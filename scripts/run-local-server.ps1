param(
    [string]$ConfigFile,
    [switch]$WithWeb
)
$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path -Parent $PSScriptRoot
if (-not $ConfigFile) { $ConfigFile = Join-Path $repositoryRoot 'secrets.properties' }
$env:ECOLACTEA_CONFIG_FILE = (Resolve-Path -LiteralPath $ConfigFile).Path
if ($WithWeb) {
    $webDirectory = Join-Path $repositoryRoot 'app/webApp/build/dist/composeWebCompatibility/productionExecutable'
    if (-not (Test-Path -LiteralPath (Join-Path $webDirectory 'index.html'))) {
        throw 'Build :app:webApp:composeCompatibilityBrowserDistribution first.'
    }
    $env:WEB_ROOT = $webDirectory
}
Push-Location $repositoryRoot
try {
    & .\gradlew.bat :server:run
    if ($LASTEXITCODE -ne 0) { throw 'Server execution failed. Review the sanitized server output.' }
} finally { Pop-Location }
