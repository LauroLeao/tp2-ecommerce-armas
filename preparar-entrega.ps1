param(
    [string]$Destino = (Join-Path (Split-Path -Parent $PSScriptRoot) 'ecommerce-armas-entrega.zip')
)

$projeto = [System.IO.Path]::GetFullPath($PSScriptRoot)
$destinoCompleto = [System.IO.Path]::GetFullPath($Destino)
$pastasIgnoradas = @('node_modules', 'dist', 'target', '.angular', '.vscode', '.git', '.github')

if (Test-Path -LiteralPath $destinoCompleto) {
    Remove-Item -LiteralPath $destinoCompleto -Force
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$arquivoZip = [System.IO.Compression.ZipFile]::Open(
    $destinoCompleto,
    [System.IO.Compression.ZipArchiveMode]::Create
)

try {
    Get-ChildItem -LiteralPath $projeto -Recurse -File | ForEach-Object {
        $caminhoRelativo = [System.IO.Path]::GetRelativePath($projeto, $_.FullName)
        $partes = $caminhoRelativo -split '[\\/]'
        $ignorar = $partes | Where-Object { $_ -in $pastasIgnoradas }

        if ($_.Extension -ne '.zip' -and -not $ignorar) {
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                $arquivoZip,
                $_.FullName,
                $caminhoRelativo,
                [System.IO.Compression.CompressionLevel]::Optimal
            ) | Out-Null
        }
    }
}
finally {
    $arquivoZip.Dispose()
}

Write-Output "Arquivo criado: $destinoCompleto"
