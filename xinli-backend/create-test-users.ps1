# 创建前端测试用户脚本

Write-Host "创建前端测试用户..." -ForegroundColor Yellow

# 测试用户1
$user1 = @{
    phone = "13800138000"
    password = "123456"
    nickname = "测试用户1"
} | ConvertTo-Json

Write-Host "`n创建用户1: 13800138000" -ForegroundColor Cyan
try {
    $response1 = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/register' -Method POST -ContentType 'application/json' -Body $user1
    Write-Host "用户1创建成功:" -ForegroundColor Green
    Write-Host $response1.Content -ForegroundColor White
} catch {
    Write-Host "用户1创建失败:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误详情: $responseBody" -ForegroundColor Red
    }
}

# 测试用户2
$user2 = @{
    phone = "13800138001"
    password = "123456"
    nickname = "测试用户2"
} | ConvertTo-Json

Write-Host "`n创建用户2: 13800138001" -ForegroundColor Cyan
try {
    $response2 = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/register' -Method POST -ContentType 'application/json' -Body $user2
    Write-Host "用户2创建成功:" -ForegroundColor Green
    Write-Host $response2.Content -ForegroundColor White
} catch {
    Write-Host "用户2创建失败:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误详情: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n测试用户创建完成!" -ForegroundColor Green
