import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E テスト設定
 * JSF Todo App (FlashContainer 状態管理)
 * テストは test-server.js (port 8080) に対して実行する
 */
export default defineConfig({
  testDir: './tests/e2e',

  /* 並列実行 */
  fullyParallel: false,

  /* CI 環境では retryしない */
  retries: 0,

  /* テストタイムアウト */
  timeout: 30_000,

  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],

  use: {
    /** テスト対象ベースURL */
    baseURL: 'http://localhost:8080',

    /** 各テストで失敗時にスクリーンショットを取得 */
    screenshot: 'only-on-failure',

    /** DOM スナップショット */
    trace: 'on-first-retry',

    /** ロケールを日本語に */
    locale: 'ja-JP',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  /**
   * テスト実行前に test-server.js を自動起動する。
   * (実際の JSF/Tomcat デプロイ環境では不要)
   */
  webServer: {
    command: 'node test-server.js',
    url: 'http://localhost:8080',
    reuseExistingServer: true,
    timeout: 10_000,
  },
});
