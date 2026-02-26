const express = require('express');
const { GetCostAndUsageCommand, GetCostForecastCommand } = require('@aws-sdk/client-cost-explorer');
const { DescribeBudgetsCommand } = require('@aws-sdk/client-budgets');
const { authenticate } = require('../middleware/auth');
const { getCostExplorerClient, getBudgetsClient, getCredentials } = require('../services/awsClient');

const router = express.Router();

// Get current month cost
router.get('/current', authenticate, async (req, res) => {
  try {
    const ce = getCostExplorerClient(req.userId);
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
    const today = now.toISOString().split('T')[0];

    const data = await ce.send(new GetCostAndUsageCommand({
      TimePeriod: { Start: startOfMonth, End: today },
      Granularity: 'MONTHLY',
      Metrics: ['UnblendedCost'],
      GroupBy: [{ Type: 'DIMENSION', Key: 'SERVICE' }],
    }));

    const results = data.ResultsByTime?.[0];
    const totalCost = results?.Total?.UnblendedCost?.Amount || '0';
    const currency = results?.Total?.UnblendedCost?.Unit || 'USD';

    const byService = (results?.Groups || []).map(g => ({
      service: g.Keys?.[0],
      cost: parseFloat(g.Metrics?.UnblendedCost?.Amount || '0').toFixed(4),
      unit: g.Metrics?.UnblendedCost?.Unit,
    })).sort((a, b) => parseFloat(b.cost) - parseFloat(a.cost));

    res.json({
      period: { start: startOfMonth, end: today },
      totalCost: parseFloat(totalCost).toFixed(4),
      currency,
      byService,
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get last 6 months cost trend
router.get('/trend', authenticate, async (req, res) => {
  try {
    const ce = getCostExplorerClient(req.userId);
    const now = new Date();

    // Go back 6 months
    const start = new Date(now.getFullYear(), now.getMonth() - 5, 1).toISOString().split('T')[0];
    const end = now.toISOString().split('T')[0];

    const data = await ce.send(new GetCostAndUsageCommand({
      TimePeriod: { Start: start, End: end },
      Granularity: 'MONTHLY',
      Metrics: ['UnblendedCost'],
    }));

    const trend = (data.ResultsByTime || []).map(r => ({
      period: r.TimePeriod?.Start,
      cost: parseFloat(r.Total?.UnblendedCost?.Amount || '0').toFixed(2),
      unit: r.Total?.UnblendedCost?.Unit || 'USD',
    }));

    res.json({ trend });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get budgets
router.get('/budgets', authenticate, async (req, res) => {
  try {
    const budgetsClient = getBudgetsClient(req.userId);
    const creds = getCredentials(req.userId);

    const data = await budgetsClient.send(new DescribeBudgetsCommand({
      AccountId: creds.accountId,
    }));

    const budgets = (data.Budgets || []).map(b => ({
      name: b.BudgetName,
      type: b.BudgetType,
      limit: {
        amount: b.BudgetLimit?.Amount,
        unit: b.BudgetLimit?.Unit,
      },
      actual: {
        amount: b.CalculatedSpend?.ActualSpend?.Amount,
        unit: b.CalculatedSpend?.ActualSpend?.Unit,
      },
      forecast: {
        amount: b.CalculatedSpend?.ForecastedSpend?.Amount,
        unit: b.CalculatedSpend?.ForecastedSpend?.Unit,
      },
      timeUnit: b.TimeUnit,
    }));

    res.json({ budgets, total: budgets.length });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
