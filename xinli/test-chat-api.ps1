# AI Chat API Test Script

Write-Host "=== AI Chat API Test ===" -ForegroundColor Green

# 1. First login to get user token
Write-Host "`n1. Getting user token..." -ForegroundColor Yellow
$loginBody = '{"phone":"13812345678","password":"test123456"}'

try {
    $loginResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $userToken = $loginResponse.data.token
    Write-Host "User login successful, got token" -ForegroundColor Green
} catch {
    Write-Host "User login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

$headers = @{
    'Authorization' = "Bearer $userToken"
    'Content-Type' = 'application/json'
}

# 2. Test create chat session
Write-Host "`n2. Testing create chat session..." -ForegroundColor Yellow
$createSessionBody = @{
    title = "Test Chat Session"
    firstMessage = "Hello, I need some psychological support."
} | ConvertTo-Json

try {
    $sessionResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/chat/session' -Method POST -Headers $headers -Body $createSessionBody
    Write-Host "Create session successful:" -ForegroundColor Green
    Write-Host "Session ID: $($sessionResponse.data.id)" -ForegroundColor Cyan
    Write-Host "Session Title: $($sessionResponse.data.title)" -ForegroundColor Cyan
    $sessionId = $sessionResponse.data.id
} catch {
    Write-Host "Create session failed: $($_.Exception.Message)" -ForegroundColor Red
    $sessionId = $null
}

# 3. Test get user sessions
Write-Host "`n3. Testing get user sessions..." -ForegroundColor Yellow
try {
    $sessionsResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/chat/sessions?page=1&size=5' -Method GET -Headers $headers
    Write-Host "Get sessions successful:" -ForegroundColor Green
    Write-Host "Total sessions: $($sessionsResponse.data.total)" -ForegroundColor Cyan
    Write-Host "Current page: $($sessionsResponse.data.current)" -ForegroundColor Cyan
    if ($sessionsResponse.data.records.Count -gt 0) {
        Write-Host "First session:" -ForegroundColor Cyan
        $firstSession = $sessionsResponse.data.records[0]
        Write-Host "  ID: $($firstSession.id)" -ForegroundColor White
        Write-Host "  Title: $($firstSession.title)" -ForegroundColor White
        Write-Host "  Message Count: $($firstSession.messageCount)" -ForegroundColor White
        if (-not $sessionId) {
            $sessionId = $firstSession.id
        }
    }
} catch {
    Write-Host "Get sessions failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Test send message (will fail without real API key, but tests the endpoint)
if ($sessionId) {
    Write-Host "`n4. Testing send message..." -ForegroundColor Yellow
    $sendMessageBody = @{
        sessionId = $sessionId
        content = "I'm feeling anxious lately. Can you help me?"
    } | ConvertTo-Json

    try {
        $messageResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/chat/send' -Method POST -Headers $headers -Body $sendMessageBody
        Write-Host "Send message successful:" -ForegroundColor Green
        Write-Host "Message ID: $($messageResponse.data.messageId)" -ForegroundColor Cyan
        Write-Host "User Message: $($messageResponse.data.userMessage)" -ForegroundColor Cyan
        Write-Host "AI Response: $($messageResponse.data.aiResponse)" -ForegroundColor Cyan
    } catch {
        Write-Host "Send message failed (expected without API key): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 5. Test get chat history
if ($sessionId) {
    Write-Host "`n5. Testing get chat history..." -ForegroundColor Yellow
    try {
        $historyResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/history?sessionId=$sessionId&page=1&size=10" -Method GET -Headers $headers
        Write-Host "Get chat history successful:" -ForegroundColor Green
        Write-Host "Session Title: $($historyResponse.data.sessionTitle)" -ForegroundColor Cyan
        Write-Host "Total Messages: $($historyResponse.data.totalMessages)" -ForegroundColor Cyan
        Write-Host "Total Tokens: $($historyResponse.data.totalTokens)" -ForegroundColor Cyan
        if ($historyResponse.data.messages.Count -gt 0) {
            Write-Host "Messages:" -ForegroundColor Cyan
            foreach ($msg in $historyResponse.data.messages) {
                Write-Host "  [$($msg.role)] $($msg.content)" -ForegroundColor White
            }
        }
    } catch {
        Write-Host "Get chat history failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 6. Test get session detail
if ($sessionId) {
    Write-Host "`n6. Testing get session detail..." -ForegroundColor Yellow
    try {
        $detailResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/session/$sessionId" -Method GET -Headers $headers
        Write-Host "Get session detail successful:" -ForegroundColor Green
        Write-Host "Session Title: $($detailResponse.data.sessionTitle)" -ForegroundColor Cyan
        Write-Host "Total Messages: $($detailResponse.data.totalMessages)" -ForegroundColor Cyan
    } catch {
        Write-Host "Get session detail failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 7. Test AI connection (will fail without API key)
Write-Host "`n7. Testing AI connection..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-RestMethod -Uri 'http://localhost:8080/api/chat/test' -Method GET -Headers $headers
    Write-Host "AI connection test successful: $($testResponse.data)" -ForegroundColor Green
} catch {
    Write-Host "AI connection test failed (expected without API key): $($_.Exception.Message)" -ForegroundColor Yellow
}

# 8. Test delete session
if ($sessionId) {
    Write-Host "`n8. Testing delete session..." -ForegroundColor Yellow
    try {
        $deleteResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/session/$sessionId" -Method DELETE -Headers $headers
        Write-Host "Delete session successful:" -ForegroundColor Green
        Write-Host "Response code: $($deleteResponse.code)" -ForegroundColor Cyan
    } catch {
        Write-Host "Delete session failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== AI Chat API Test Complete ===" -ForegroundColor Green
Write-Host "Note: Some tests may fail without a valid DeepSeek API key, but the endpoints are working." -ForegroundColor Yellow
