param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$Users = 200,
    [int]$GameId = 1,
    [int]$Quantity = 1,
    [string]$UsernamePrefix = "stress_user_",
    [string]$Password = "stress123",
    [int]$Concurrency = 64
)

$ErrorActionPreference = "Stop"
$started = Get-Date
$pool = [runspacefactory]::CreateRunspacePool(1, $Concurrency)
$pool.Open()
$jobs = New-Object System.Collections.Generic.List[object]

$scriptBlock = {
    param($BaseUrl, $Index, $GameId, $Quantity, $UsernamePrefix, $Password)

    $registerUrl = "$BaseUrl/api/auth/register"
    $loginUrl = "$BaseUrl/api/auth/login"
    $orderUrl = "$BaseUrl/api/orders"
    $username = "$UsernamePrefix$Index"

    try {
        $registerBody = @{ username = $username; password = $Password; nickname = $username } | ConvertTo-Json
        try {
            Invoke-RestMethod -Method Post -Uri $registerUrl -ContentType "application/json" -Body $registerBody | Out-Null
        } catch {
            # Existing stress users are fine; the login below proves the account is usable.
        }

        $loginBody = @{ username = $username; password = $Password } | ConvertTo-Json
        $login = Invoke-RestMethod -Method Post -Uri $loginUrl -ContentType "application/json" -Body $loginBody
        if ($null -eq $login.data.token) {
            return [pscustomobject]@{ ok = $false; user = $username; message = "login failed" }
        }

        $headers = @{ Authorization = "Bearer $($login.data.token)" }
        $orderBody = @{
            fromCart = $false
            idempotencyKey = "stress-$username-$(Get-Random)"
            items = @(@{ gameId = $GameId; quantity = $Quantity })
        } | ConvertTo-Json -Depth 5

        $response = Invoke-RestMethod -Method Post -Uri $orderUrl -Headers $headers -ContentType "application/json" -Body $orderBody
        return [pscustomobject]@{ ok = ($response.code -eq 0); user = $username; message = $response.message }
    } catch {
        return [pscustomobject]@{ ok = $false; user = $username; message = $_.Exception.Message }
    }
}

for ($i = 1; $i -le $Users; $i++) {
    $ps = [powershell]::Create()
    $ps.RunspacePool = $pool
    [void]$ps.AddScript($scriptBlock).AddArgument($BaseUrl).AddArgument($i).AddArgument($GameId).AddArgument($Quantity).AddArgument($UsernamePrefix).AddArgument($Password)
    $jobs.Add([pscustomobject]@{ shell = $ps; handle = $ps.BeginInvoke() })
}

$results = New-Object System.Collections.Generic.List[object]
foreach ($job in $jobs) {
    $output = $job.shell.EndInvoke($job.handle)
    foreach ($item in $output) {
        $results.Add($item)
    }
    $job.shell.Dispose()
}
$pool.Close()
$pool.Dispose()

$elapsed = ((Get-Date) - $started).TotalSeconds
$success = @($results | Where-Object { $_.ok }).Count
$failed = @($results | Where-Object { -not $_.ok }).Count

[pscustomobject]@{
    users = $Users
    concurrency = $Concurrency
    success = $success
    failed = $failed
    elapsedSeconds = [math]::Round($elapsed, 2)
    throughput = [math]::Round($Users / [math]::Max($elapsed, 0.001), 2)
}

$results |
    Where-Object { -not $_.ok } |
    Group-Object message |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Format-Table -AutoSize
