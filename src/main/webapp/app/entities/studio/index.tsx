import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Studio from './studio';
import StudioDetail from './studio-detail';
import StudioUpdate from './studio-update';
import StudioDeleteDialog from './studio-delete-dialog';

const StudioRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Studio />} />
    <Route path="new" element={<StudioUpdate />} />
    <Route path=":id">
      <Route index element={<StudioDetail />} />
      <Route path="edit" element={<StudioUpdate />} />
      <Route path="delete" element={<StudioDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default StudioRoutes;
