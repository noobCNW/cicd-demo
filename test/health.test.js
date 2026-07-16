const { describe, it } = require('node:test');
const assert = require('node:assert');

describe('cicd-demo', () => {
  it('version is semver-like', () => {
    assert.match(require('../package.json').version, /^\d+\.\d+\.\d+$/);
  });
});
