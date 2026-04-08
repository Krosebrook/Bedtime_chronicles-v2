import { describe, it, expect } from 'vitest';
import { toErrorMessage } from './utils';

describe('toErrorMessage', () => {
  it('extracts message from Error objects', () => {
    expect(toErrorMessage(new Error('something broke'))).toBe('something broke');
  });

  it('converts non-Error values to string', () => {
    expect(toErrorMessage('raw string')).toBe('raw string');
    expect(toErrorMessage(42)).toBe('42');
    expect(toErrorMessage(null)).toBe('null');
    expect(toErrorMessage(undefined)).toBe('undefined');
  });

  it('handles objects without message property', () => {
    expect(toErrorMessage({ code: 'ENOENT' })).toBe('[object Object]');
  });
});
