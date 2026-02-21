$token = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiYWRtaW4iLCJ1c2VySWQiOjEsInN1YiI6ImFkbWluIiwiaWF0IjoxNzUyNzM3MDMzLCJleHAiOjE3NTI4MjM0MzN9.-DvMRYOYDxFeXGNEjW46E9C8mwYDXDI0wVcLdjRJvDEPNC2iLLIJx7yrtGGMcDF8AMOkiRheEzqq3ciQRYF7iPA"

try {
    $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/posts?page=1&size=3' -Method GET -Headers @{'Authorization'="Bearer $token"}
    Write-Host "API响应成功!"
    Write-Host "响应数据:"
    $response | ConvertTo-Json -Depth 5
} catch {
    Write-Host "API调用失败: $($_.Exception.Message)"
}
