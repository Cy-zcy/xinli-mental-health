# 用户端论坛接口测试脚本

Write-Host "=== 用户端论坛接口测试 ===" -ForegroundColor Green

# 1. 先获取管理员token（用于测试，实际应该是用户token）
Write-Host "`n1. 获取认证token..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginResponse.data.token
    Write-Host "✅ 登录成功，获取到token" -ForegroundColor Green
} catch {
    Write-Host "❌ 登录失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    'Authorization' = "Bearer $token"
    'Content-Type' = 'application/json'
}

# 2. 测试获取论坛分类列表
Write-Host "`n2. 测试获取论坛分类列表..." -ForegroundColor Yellow
try {
    $categoriesResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/categories' -Method GET
    Write-Host "✅ 获取分类列表成功:" -ForegroundColor Green
    $categoriesResponse.data | ForEach-Object {
        Write-Host "   - $($_.label) ($($_.value)): $($_.description)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "❌ 获取分类列表失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 测试用户端获取帖子列表
Write-Host "`n3. 测试用户端获取帖子列表..." -ForegroundColor Yellow
try {
    $postsResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts?page=1&size=5' -Method GET
    Write-Host "✅ 获取帖子列表成功:" -ForegroundColor Green
    Write-Host "   总数: $($postsResponse.data.total)" -ForegroundColor Cyan
    Write-Host "   当前页: $($postsResponse.data.current)" -ForegroundColor Cyan
    Write-Host "   帖子列表:" -ForegroundColor Cyan
    $postsResponse.data.records | ForEach-Object {
        Write-Host "   - [$($_.id)] $($_.title) (👍$($_.likeCount) 👁$($_.viewCount))" -ForegroundColor White
        Write-Host "     作者: $($_.author.nickname) | 分类: $($_.category) | 时间: $($_.createdAt)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 获取帖子列表失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试发布帖子
Write-Host "`n4. 测试发布帖子..." -ForegroundColor Yellow
$newPostBody = @{
    title = "Test Post - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    content = "This is a test post content to verify user forum posting functionality. Content needs at least 10 characters to pass validation."
    category = "experience"
} | ConvertTo-Json

try {
    $createPostResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts' -Method POST -Headers $headers -Body $newPostBody
    $newPostId = $createPostResponse.data.id
    Write-Host "✅ 发布帖子成功:" -ForegroundColor Green
    Write-Host "   帖子ID: $newPostId" -ForegroundColor Cyan
    Write-Host "   标题: $($createPostResponse.data.title)" -ForegroundColor Cyan
    Write-Host "   分类: $($createPostResponse.data.category)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 发布帖子失败: $($_.Exception.Message)" -ForegroundColor Red
    $newPostId = 1  # 使用现有帖子ID进行后续测试
}

# 5. 测试获取帖子详情
Write-Host "`n5. 测试获取帖子详情..." -ForegroundColor Yellow
try {
    $postDetailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId" -Method GET -Headers $headers
    Write-Host "✅ 获取帖子详情成功:" -ForegroundColor Green
    Write-Host "   标题: $($postDetailResponse.data.title)" -ForegroundColor Cyan
    Write-Host "   内容: $($postDetailResponse.data.content.Substring(0, [Math]::Min(50, $postDetailResponse.data.content.Length)))..." -ForegroundColor Cyan
    Write-Host "   作者: $($postDetailResponse.data.author.nickname)" -ForegroundColor Cyan
    Write-Host "   点赞数: $($postDetailResponse.data.likeCount)" -ForegroundColor Cyan
    Write-Host "   浏览数: $($postDetailResponse.data.viewCount)" -ForegroundColor Cyan
    Write-Host "   是否已点赞: $($postDetailResponse.data.liked)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 获取帖子详情失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. 测试点赞帖子
Write-Host "`n6. 测试点赞帖子..." -ForegroundColor Yellow
try {
    $likeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId/like" -Method POST -Headers $headers
    Write-Host "✅ 点赞成功:" -ForegroundColor Green
    Write-Host "   消息: $($likeResponse.data.message)" -ForegroundColor Cyan
    Write-Host "   点赞状态: $($likeResponse.data.liked)" -ForegroundColor Cyan
    Write-Host "   点赞数: $($likeResponse.data.likeCount)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 点赞失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. 测试取消点赞
Write-Host "`n7. 测试取消点赞..." -ForegroundColor Yellow
try {
    $unlikeResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/forum/posts/$newPostId/like" -Method DELETE -Headers $headers
    Write-Host "✅ 取消点赞成功:" -ForegroundColor Green
    Write-Host "   消息: $($unlikeResponse.data.message)" -ForegroundColor Cyan
    Write-Host "   点赞状态: $($unlikeResponse.data.liked)" -ForegroundColor Cyan
    Write-Host "   点赞数: $($unlikeResponse.data.likeCount)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 取消点赞失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. 测试按分类筛选帖子
Write-Host "`n8. 测试按分类筛选帖子..." -ForegroundColor Yellow
try {
    $filteredPostsResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts?category=experience&page=1&size=3' -Method GET
    Write-Host "✅ 按分类筛选成功:" -ForegroundColor Green
    Write-Host "   经验分享类帖子数量: $($filteredPostsResponse.data.total)" -ForegroundColor Cyan
    $filteredPostsResponse.data.records | ForEach-Object {
        Write-Host "   - [$($_.id)] $($_.title) (分类: $($_.category))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 按分类筛选失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 9. 测试关键词搜索
Write-Host "`n9. 测试关键词搜索..." -ForegroundColor Yellow
try {
    $searchResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/forum/posts?keyword=心理&page=1&size=3' -Method GET
    Write-Host "✅ 关键词搜索成功:" -ForegroundColor Green
    Write-Host "   搜索'心理'的结果数量: $($searchResponse.data.total)" -ForegroundColor Cyan
    $searchResponse.data.records | ForEach-Object {
        Write-Host "   - [$($_.id)] $($_.title)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 关键词搜索失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 用户端论坛接口测试完成 ===" -ForegroundColor Green
