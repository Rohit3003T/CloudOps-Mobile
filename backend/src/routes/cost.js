'use strict';
const express = require('express');
const { GetCostAndUsageCommand } = require('@aws-sdk/client-cost-explorer');
const { costClient, isCredentialError } = require('../aws/clients');
const MOCK = require('../mock/costMock');

const router = express.Router();

/**
 * GET /api/cost
 * Month-to-date AWS cost breakdown by service.
 * Optional query params:
 *   ?granularity=MONTHLY|DAILY   (default MONTHLY)
 *   ?months=1                    (how many months back, default 1)
 */
router.get('/', async (req, res, next) => {
  try {
    const granularity = req.query.granularity === 'DAILY' ? 'DAILY' : 'MONTHLY';
    const monthsBack  = Math.min(Math.max(parseInt(req.query.months || '1', 10), 1), 12);

    const now          = new Date();
    const endDate      = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startDate    = new Date(now.getFullYear(), now.getMonth() - (monthsBack - 1), 1);

    // Cost Explorer requires end > start
    if (endDate <= startDate) endDate.setDate(endDate.getDate() + 1);

    const fmt = (d) => d.toISOString().slice(0, 10);

    const command = new GetCostAndUsageCommand({
      TimePeriod:   { Start: fmt(startDate), End: fmt(endDate) },
      Granularity:  granularity,
      Metrics:      ['BlendedCost'],
      GroupBy:      [{ Type: 'DIMENSION', Key: 'SERVICE' }],
    });

    const response = await costClient.send(command);

    // Aggregate across all time periods
    const serviceMap = {};
    for (const period of response.ResultsByTime || []) {
      for (const group of period.Groups || []) {
        const svc    = group.Keys?.[0] || 'Unknown';
        const amount = parseFloat(group.Metrics?.BlendedCost?.Amount || '0');
        if (amount > 0) serviceMap[svc] = (serviceMap[svc] || 0) + amount;
      }
    }

    const services = Object.entries(serviceMap)
      .map(([service, amount]) => ({ service, amount: amount.toFixed(4), unit: 'USD' }))
      .sort((a, b) => parseFloat(b.amount) - parseFloat(a.amount));

    const totalCost = services.reduce((s, i) => s + parseFloat(i.amount), 0);

    res.json({
      success:     true,
      granularity,
      period:      { start: fmt(startDate), end: fmt(endDate) },
      totalCost:   totalCost.toFixed(4),
      currency:    'USD',
      serviceCount: services.length,
      services,
    });
  } catch (err) {
    if (isCredentialError(err) || err.name === 'AccessDeniedException') {
      return res.json(MOCK.costSummary());
    }
    next(err);
  }
});

module.exports = router;
