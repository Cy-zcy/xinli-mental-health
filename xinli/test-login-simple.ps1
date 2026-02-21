$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/auth/login" -Method POST -ContentType "application/json" -Body $body
    Write-Host "登录成功!"
    Write-Host "响应数据:"
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "登录失败:"
    Write-Host $_.Exception.Message
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        Write-Host "错误详情:" $reader.ReadToEnd()
    }
}
