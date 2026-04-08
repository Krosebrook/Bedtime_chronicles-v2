import { defineConfig } from 'vitest/config';
import path from 'path';

export default defineConfig({
  resolve: {
    alias: {
      // Zod 3.25+ has broken ESM exports on Windows — force CJS entry
      zod: path.resolve(__dirname, 'node_modules/zod/index.cjs'),
    },
  },
  test: {
    globals: true,
    environment: 'node',
    include: ['server/**/*.test.ts', 'lib/**/*.test.ts'],
    exclude: ['node_modules', 'server_dist', 'static-build'],
    coverage: {
      provider: 'v8',
      include: ['server/**/*.ts', 'lib/**/*.ts'],
      exclude: [
        'server/replit_integrations/**',
        'server/templates/**',
        '**/*.test.ts',
        '**/index.ts',
      ],
      thresholds: {
        branches: 80,
      },
    },
    testTimeout: 10000,
  },
});
