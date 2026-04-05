'use strict';

const GITHUB_API = 'https://api.github.com';

function githubHeaders() {
  const token = process.env.GITHUB_TOKEN;
  if (!token) throw Object.assign(new Error('GITHUB_TOKEN is not configured'), { statusCode: 501 });
  return {
    Authorization: `Bearer ${token}`,
    Accept:        'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
    'User-Agent':  'cloud-monitor-backend/2.0',
  };
}

async function ghFetch(path) {
  // dynamic import for node-fetch v3 (ESM)
  const { default: fetch } = await import('node-fetch');
  const resp = await fetch(`${GITHUB_API}${path}`, { headers: githubHeaders() });
  if (!resp.ok) {
    const body = await resp.text();
    const err  = new Error(`GitHub API error ${resp.status}: ${body}`);
    err.statusCode = resp.status;
    throw err;
  }
  return resp.json();
}

/**
 * List recent workflow runs for a repo.
 */
async function listWorkflowRuns(owner, repo, { perPage = 10, branch, status } = {}) {
  let path = `/repos/${owner}/${repo}/actions/runs?per_page=${perPage}`;
  if (branch) path += `&branch=${encodeURIComponent(branch)}`;
  if (status) path += `&status=${encodeURIComponent(status)}`;

  const data = await ghFetch(path);
  return {
    totalCount: data.total_count,
    runs: (data.workflow_runs || []).map(mapRun),
  };
}

/**
 * Get a single workflow run by ID.
 */
async function getWorkflowRun(owner, repo, runId) {
  const data = await ghFetch(`/repos/${owner}/${repo}/actions/runs/${runId}`);
  return mapRun(data);
}

/**
 * List all workflows defined in a repo.
 */
async function listWorkflows(owner, repo) {
  const data = await ghFetch(`/repos/${owner}/${repo}/actions/workflows`);
  return {
    totalCount: data.total_count,
    workflows: (data.workflows || []).map(w => ({
      id:        w.id,
      name:      w.name,
      state:     w.state,
      path:      w.path,
      createdAt: w.created_at,
      updatedAt: w.updated_at,
      url:       w.html_url,
    })),
  };
}

/**
 * Get jobs for a specific workflow run.
 */
async function getRunJobs(owner, repo, runId) {
  const data = await ghFetch(`/repos/${owner}/${repo}/actions/runs/${runId}/jobs`);
  return {
    totalCount: data.total_count,
    jobs: (data.jobs || []).map(j => ({
      id:          j.id,
      name:        j.name,
      status:      j.status,
      conclusion:  j.conclusion,
      startedAt:   j.started_at,
      completedAt: j.completed_at,
      steps: (j.steps || []).map(s => ({
        name:        s.name,
        status:      s.status,
        conclusion:  s.conclusion,
        number:      s.number,
        startedAt:   s.started_at,
        completedAt: s.completed_at,
      })),
    })),
  };
}

function mapRun(r) {
  return {
    id:           r.id,
    name:         r.name,
    workflowId:   r.workflow_id,
    status:       r.status,       // queued | in_progress | completed
    conclusion:   r.conclusion,   // success | failure | cancelled | skipped | null
    branch:       r.head_branch,
    commitSha:    r.head_sha?.slice(0, 8),
    commitMessage: r.head_commit?.message?.split('\n')[0] || null,
    actor:        r.actor?.login || null,
    triggerEvent: r.event,
    runNumber:    r.run_number,
    runAttempt:   r.run_attempt,
    createdAt:    r.created_at,
    updatedAt:    r.updated_at,
    url:          r.html_url,
    durationMs:   r.created_at && r.updated_at
      ? new Date(r.updated_at) - new Date(r.created_at)
      : null,
  };
}

module.exports = { listWorkflowRuns, getWorkflowRun, listWorkflows, getRunJobs };
