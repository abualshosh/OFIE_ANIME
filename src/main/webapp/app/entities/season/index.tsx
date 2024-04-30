import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Season from './season';
import SeasonDetail from './season-detail';
import SeasonUpdate from './season-update';
import SeasonDeleteDialog from './season-delete-dialog';

const SeasonRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Season />} />
    <Route path="new" element={<SeasonUpdate />} />
    <Route path=":id">
      <Route index element={<SeasonDetail />} />
      <Route path="edit" element={<SeasonUpdate />} />
      <Route path="delete" element={<SeasonDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SeasonRoutes;
