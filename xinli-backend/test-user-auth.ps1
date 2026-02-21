# User Authentication API Test Script

Write-Host "=== User Authentication API Test ===" -ForegroundColor Green

$timestamp = Get-Date -Format "MMddHHmm"
$testPhone = "139${timestamp}".Substring(0, 11)  # Generate unique phone number
$testPassword = "test123456"
$testNickname = "User$timestamp"

# 1. Test user registration
Write-Host "`n1. Testing user registration..." -ForegroundColor Yellow
$registerBody = @{
    phone = $testPhone
    password = $testPassword
    nickname = $testNickname
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/register' -Method POST -ContentType 'application/json' -Body $registerBody
    Write-Host "User registration successful:" -ForegroundColor Green
    Write-Host "User ID: $($registerResponse.data.id)" -ForegroundColor Cyan
    Write-Host "Phone: $($registerResponse.data.phone)" -ForegroundColor Cyan
    Write-Host "Nickname: $($registerResponse.data.nickname)" -ForegroundColor Cyan
    $userId = $registerResponse.data.id
} catch {
    Write-Host "User registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Test check phone exists
Write-Host "`n2. Testing check phone exists..." -ForegroundColor Yellow
try {
    $checkPhoneResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/check-phone?phone=$testPhone" -Method GET
    Write-Host "Check phone exists successful:" -ForegroundColor Green
    Write-Host "Phone exists: $($checkPhoneResponse.data)" -ForegroundColor Cyan
} catch {
    Write-Host "Check phone exists failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Test user login
Write-Host "`n3. Testing user login..." -ForegroundColor Yellow
$loginBody = @{
    phone = $testPhone
    password = $testPassword
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    Write-Host "User login successful:" -ForegroundColor Green
    Write-Host "Token: $($loginResponse.data.token.Substring(0, 20))..." -ForegroundColor Cyan
    Write-Host "Token Type: $($loginResponse.data.tokenType)" -ForegroundColor Cyan
    Write-Host "Expires In: $($loginResponse.data.expiresIn) seconds" -ForegroundColor Cyan
    Write-Host "User Info:" -ForegroundColor Cyan
    Write-Host "  ID: $($loginResponse.data.user.id)" -ForegroundColor White
    Write-Host "  Phone: $($loginResponse.data.user.phone)" -ForegroundColor White
    Write-Host "  Nickname: $($loginResponse.data.user.nickname)" -ForegroundColor White
    Write-Host "  Status: $($loginResponse.data.user.status)" -ForegroundColor White
    
    $userToken = $loginResponse.data.token
} catch {
    Write-Host "User login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 4. Test get current user info (using existing endpoint)
Write-Host "`n4. Testing get current user info..." -ForegroundColor Yellow
$headers = @{
    'Authorization' = "Bearer $userToken"
    'Content-Type' = 'application/json'
}

try {
    $userInfoResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/user/info' -Method GET -Headers $headers
    Write-Host "Get user info successful:" -ForegroundColor Green
    Write-Host "User ID: $($userInfoResponse.data.id)" -ForegroundColor Cyan
    Write-Host "Username: $($userInfoResponse.data.username)" -ForegroundColor Cyan
    Write-Host "Name: $($userInfoResponse.data.name)" -ForegroundColor Cyan
    Write-Host "Role: $($userInfoResponse.data.role)" -ForegroundColor Cyan
} catch {
    Write-Host "Get user info failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Test update user info - nickname only
Write-Host "`n5. Testing update user info (nickname only)..." -ForegroundColor Yellow
$updateBody1 = @{
    nickname = "New$timestamp"
} | ConvertTo-Json

try {
    $updateResponse1 = Invoke-RestMethod -Uri 'http://localhost:8080/api/user/info' -Method PUT -Headers $headers -Body $updateBody1
    Write-Host "Update user nickname successful:" -ForegroundColor Green
    Write-Host "New nickname: $($updateResponse1.data.nickname)" -ForegroundColor Cyan
} catch {
    Write-Host "Update user nickname failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Test update user info - avatar only
Write-Host "`n6. Testing update user info (avatar only)..." -ForegroundColor Yellow
$updateBody2 = @{
    avatar = "https://example.com/new-avatar.jpg"
} | ConvertTo-Json

try {
    $updateResponse2 = Invoke-RestMethod -Uri 'http://localhost:8080/api/user/info' -Method PUT -Headers $headers -Body $updateBody2
    Write-Host "Update user avatar successful:" -ForegroundColor Green
    Write-Host "New avatar: $($updateResponse2.data.avatar)" -ForegroundColor Cyan
} catch {
    Write-Host "Update user avatar failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Test update password
Write-Host "`n7. Testing update password..." -ForegroundColor Yellow
$newPassword = "newpass123456"
$updatePasswordBody = @{
    oldPassword = $testPassword
    newPassword = $newPassword
} | ConvertTo-Json

try {
    $updatePasswordResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/user/info' -Method PUT -Headers $headers -Body $updatePasswordBody
    Write-Host "Update password successful:" -ForegroundColor Green
    Write-Host "User ID: $($updatePasswordResponse.data.id)" -ForegroundColor Cyan
} catch {
    Write-Host "Update password failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Test login with new password
Write-Host "`n8. Testing login with new password..." -ForegroundColor Yellow
$newLoginBody = @{
    phone = $testPhone
    password = $newPassword
} | ConvertTo-Json

try {
    $newLoginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method POST -ContentType 'application/json' -Body $newLoginBody
    Write-Host "Login with new password successful:" -ForegroundColor Green
    Write-Host "New token: $($newLoginResponse.data.token.Substring(0, 20))..." -ForegroundColor Cyan
    $newUserToken = $newLoginResponse.data.token
} catch {
    Write-Host "Login with new password failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 9. Test refresh token
Write-Host "`n9. Testing refresh token..." -ForegroundColor Yellow
$refreshHeaders = @{
    'Authorization' = "Bearer $newUserToken"
    'Content-Type' = 'application/json'
}

try {
    $refreshResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/refresh' -Method POST -Headers $refreshHeaders
    Write-Host "Refresh token successful:" -ForegroundColor Green
    Write-Host "New token: $($refreshResponse.data.token.Substring(0, 20))..." -ForegroundColor Cyan
    Write-Host "Expires in: $($refreshResponse.data.expiresIn) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "Refresh token failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 10. Test logout
Write-Host "`n10. Testing logout..." -ForegroundColor Yellow
try {
    $logoutResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/logout' -Method POST -Headers $refreshHeaders
    Write-Host "Logout successful:" -ForegroundColor Green
    Write-Host "Response code: $($logoutResponse.code)" -ForegroundColor Cyan
} catch {
    Write-Host "Logout failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 11. Test duplicate registration (should fail)
Write-Host "`n11. Testing duplicate registration (should fail)..." -ForegroundColor Yellow
try {
    $duplicateResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/register' -Method POST -ContentType 'application/json' -Body $registerBody
    Write-Host "Duplicate registration unexpectedly succeeded" -ForegroundColor Red
} catch {
    Write-Host "Duplicate registration correctly failed: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n=== User Authentication API Test Complete ===" -ForegroundColor Green
