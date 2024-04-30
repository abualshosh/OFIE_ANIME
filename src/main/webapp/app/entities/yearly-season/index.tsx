import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import YearlySeason from './yearly-season';
import YearlySeasonDetail from './yearly-season-detail';
import YearlySeasonUpdate from './yearly-season-update';
import YearlySeasonDeleteDialog from './yearly-season-delete-dialog';

const YearlySeasonRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<YearlySeason />} />
    <Route path="new" element={<YearlySeasonUpdate />} />
    <Route path=":id">
      <Route index element={<YearlySeasonDetail />} />
      <Route path="edit" element={<YearlySeasonUpdate />} />
      <Route path="delete" element={<YearlySeasonDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default YearlySeasonRoutes;
