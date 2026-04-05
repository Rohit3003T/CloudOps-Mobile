'use strict';

module.exports = {
  costSummary: () => {
    const now          = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const fmt          = (d) => d.toISOString().slice(0, 10);

    return {
      success:      true,
      mock:         true,
      granularity:  'MONTHLY',
      period:       { start: fmt(startOfMonth), end: fmt(now) },
      totalCost:    '142.8700',
      currency:     'USD',
      serviceCount: 7,
      services: [
        { service: 'Amazon Elastic Compute Cloud - Compute', amount: '68.4000', unit: 'USD' },
        { service: 'Amazon Relational Database Service',     amount: '32.1500', unit: 'USD' },
        { service: 'Amazon Simple Storage Service',          amount: '18.2200', unit: 'USD' },
        { service: 'AWS Lambda',                             amount: '9.5500',  unit: 'USD' },
        { service: 'Amazon CloudFront',                      amount: '7.3000',  unit: 'USD' },
        { service: 'Amazon Route 53',                        amount: '4.2500',  unit: 'USD' },
        { service: 'AWS Data Transfer',                      amount: '3.0000',  unit: 'USD' },
      ],
    };
  },
};
