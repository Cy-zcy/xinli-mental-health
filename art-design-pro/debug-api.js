// 简单的API调试脚本
// 在浏览器控制台中运行

async function testLoginAPI() {
    console.log('🔍 开始测试登录API...');
    
    try {
        const response = await fetch('http://localhost:8080/api/admin/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: 'admin',
                password: 'admin123'
            })
        });
        
        console.log('📡 HTTP状态码:', response.status);
        console.log('📡 响应头:', Object.fromEntries(response.headers.entries()));
        
        const data = await response.json();
        console.log('📦 响应数据:', data);
        
        if (response.ok && data.code === 200) {
            console.log('✅ 登录成功!');
            console.log('🔑 Token:', data.data.token.substring(0, 50) + '...');
            console.log('👤 管理员信息:', data.data.admin);
            
            // 测试带token的请求
            await testAuthenticatedAPI(data.data.token);
        } else {
            console.log('❌ 登录失败:', data.msg || '未知错误');
        }
    } catch (error) {
        console.error('💥 请求异常:', error);
    }
}

async function testAuthenticatedAPI(token) {
    console.log('\n🔍 测试需要认证的API...');
    
    const apis = [
        { name: '仪表板统计', url: '/api/admin/dashboard/stats' },
        { name: '用户列表', url: '/api/admin/users?page=1&size=5' },
        { name: '帖子列表', url: '/api/admin/posts?page=1&size=5' }
    ];
    
    for (const api of apis) {
        try {
            console.log(`\n📡 测试 ${api.name}...`);
            const response = await fetch(`http://localhost:8080${api.url}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            const data = await response.json();
            
            if (response.ok) {
                console.log(`✅ ${api.name} 成功:`, data);
            } else {
                console.log(`❌ ${api.name} 失败:`, data.msg || '未知错误');
            }
        } catch (error) {
            console.error(`💥 ${api.name} 异常:`, error);
        }
    }
}

// 测试前端HTTP工具
async function testFrontendHTTP() {
    console.log('\n🔍 测试前端HTTP工具...');
    
    // 这个需要在前端环境中运行
    if (typeof window !== 'undefined' && window.location.hostname === 'localhost') {
        console.log('🌐 在前端环境中，可以测试HTTP工具');
        
        // 检查是否有全局的request对象
        if (typeof request !== 'undefined') {
            console.log('📦 发现全局request对象');
        } else {
            console.log('❌ 未发现全局request对象');
        }
    } else {
        console.log('⚠️ 不在前端环境中，跳过前端HTTP工具测试');
    }
}

// 运行所有测试
async function runAllTests() {
    console.log('🚀 开始API调试测试...\n');
    
    await testLoginAPI();
    await testFrontendHTTP();
    
    console.log('\n✨ 测试完成!');
}

// 导出函数供控制台使用
if (typeof window !== 'undefined') {
    window.testLoginAPI = testLoginAPI;
    window.testAuthenticatedAPI = testAuthenticatedAPI;
    window.testFrontendHTTP = testFrontendHTTP;
    window.runAllTests = runAllTests;
    
    console.log('🎯 API调试工具已加载!');
    console.log('💡 使用方法:');
    console.log('   - runAllTests() - 运行所有测试');
    console.log('   - testLoginAPI() - 测试登录API');
    console.log('   - testFrontendHTTP() - 测试前端HTTP工具');
}

// 如果在Node.js环境中，直接运行测试
if (typeof window === 'undefined') {
    runAllTests();
}
