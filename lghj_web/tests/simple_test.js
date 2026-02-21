const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch();
    const page = await browser.newPage();

    console.log('Testing Admin Login...');

    // 1. Go to login page
    await page.goto('http://localhost:5173/login');

    // 2. Fill login form
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', '123456');

    // 3. Click login
    await page.click('button.login-btn');

    // 4. Wait for navigation
    try {
        await page.waitForURL(/\/dashboard/, { timeout: 10000 });
        const url = page.url();
        console.log('Redirected to:', url);

        if (url.includes('/admin/dashboard')) {
            console.log('✅ Admin login successful');
            const title = await page.locator('h2').innerText();
            console.log('Page Title:', title);
        } else {
            console.log('⚠️ User login successful (not admin)');
        }
    } catch (e) {
        console.log('❌ Login failed or timed out');
        console.error(e);
    }

    await browser.close();
})();