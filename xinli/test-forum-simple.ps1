# Simple User Forum API Test

Write-Host "=== User Forum API Test ===" -ForegroundColor Green

# 1. Login to get token
Write-Host "`n1. Getting auth token..." -ForegroundColor Yellow
$loginBody = '{"username":"admin","password":"admin123"}'

try {
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginResponse.data.token
    Write-Host "Login successful, got token" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    'Authorization' = "Bearer $token"
    'Content-Type' = 'application/json'
}

# 2. Test get categories
Write-Host "`n2. Testing get categories..." -ForegroundColor Yellow
try {
    $categoriesResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/categories' -Method GET
    Write-Host "Get categories successful:" -ForegroundColor Green
    $categoriesResponse.data | ConvertTo-Json
} catch {
    Write-Host "Get categories failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Test get posts list
Write-Host "`n3. Testing get posts list..." -ForegroundColor Yellow
try {
    $postsResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts?page=1&size=3' -Method GET
    Write-Host "Get posts list successful:" -ForegroundColor Green
    Write-Host "Total: $($postsResponse.data.total)" -ForegroundColor Cyan
    Write-Host "Current page: $($postsResponse.data.current)" -ForegroundColor Cyan
} catch {
    Write-Host "Get posts list failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Test create post
Write-Host "`n4. Testing create post..." -ForegroundColor Yellow
$newPostBody = '{"title":"Test Post","content":"This is a test post content for API testing.","category":"experience"}'

try {
    $createPostResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts' -Method POST -Headers $headers -Body $newPostBody
    $newPostId = $createPostResponse.data.id
    Write-Host "Create post successful:" -ForegroundColor Green
    Write-Host "Post ID: $newPostId" -ForegroundColor Cyan
} catch {
    Write-Host "Create post failed: $($_.Exception.Message)" -ForegroundColor Red
    $newPostId = 1  # Use existing post ID for further tests
}

# 5. Test get post detail
Write-Host "`n5. Testing get post detail..." -ForegroundColor Yellow
try {
    $postDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId" -Method GET -Headers $headers
    Write-Host "Get post detail successful:" -ForegroundColor Green
    Write-Host "Title: $($postDetailResponse.data.title)" -ForegroundColor Cyan
    Write-Host "Like count: $($postDetailResponse.data.likeCount)" -ForegroundColor Cyan
    Write-Host "Is liked: $($postDetailResponse.data.liked)" -ForegroundColor Cyan
} catch {
    Write-Host "Get post detail failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Test like post
Write-Host "`n6. Testing like post..." -ForegroundColor Yellow
try {
    $likeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId/like" -Method POST -Headers $headers
    Write-Host "Like post successful:" -ForegroundColor Green
    Write-Host "Message: $($likeResponse.data.message)" -ForegroundColor Cyan
    Write-Host "Like count: $($likeResponse.data.likeCount)" -ForegroundColor Cyan
} catch {
    Write-Host "Like post failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Test unlike post
Write-Host "`n7. Testing unlike post..." -ForegroundColor Yellow
try {
    $unlikeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId/like" -Method DELETE -Headers $headers
    Write-Host "Unlike post successful:" -ForegroundColor Green
    Write-Host "Message: $($unlikeResponse.data.message)" -ForegroundColor Cyan
    Write-Host "Like count: $($unlikeResponse.data.likeCount)" -ForegroundColor Cyan
} catch {
    Write-Host "Unlike post failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== User Forum API Test Complete ===" -ForegroundColor Green
