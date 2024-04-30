import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import History from './history';
import HistoryDetail from './history-detail';
import HistoryUpdate from './history-update';
import HistoryDeleteDialog from './history-delete-dialog';

const HistoryRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<History />} />
    <Route path="new" element={<HistoryUpdate />} />
    <Route path=":id">
      <Route index element={<HistoryDetail />} />
      <Route path="edit" element={<HistoryUpdate />} />
      <Route path="delete" element={<HistoryDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default HistoryRoutes;
