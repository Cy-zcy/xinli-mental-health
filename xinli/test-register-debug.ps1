# Debug user registration

$timestamp = Get-Date -Format "MMddHHmm"
$testPhone = "138${timestamp}".Substring(0, 11)
$testPassword = "test123456"
$testNickname = "Test$timestamp"

Write-Host "Testing registration with:" -ForegroundColor Yellow
Write-Host "Phone: $testPhone" -ForegroundColor Cyan
Write-Host "Password: $testPassword" -ForegroundColor Cyan
Write-Host "Nickname: $testNickname" -ForegroundColor Cyan

$registerBody = @{
    phone = $testPhone
    password = $testPassword
    nickname = $testNickname
    avatar = "https://example.com/avatar.jpg"
} | ConvertTo-Json

Write-Host "`nRequest body:" -ForegroundColor Yellow
Write-Host $registerBody -ForegroundColor White

try {
    $registerResponse = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/register' -Method POST -ContentType 'application/json' -Body $registerBody
    Write-Host "`nRegistration successful:" -ForegroundColor Green
    Write-Host $registerResponse.Content -ForegroundColor Cyan
} catch {
    Write-Host "`nRegistration failed:" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody" -ForegroundColor Red
    }
}
