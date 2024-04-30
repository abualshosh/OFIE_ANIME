import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Favirote from './favirote';
import FaviroteDetail from './favirote-detail';
import FaviroteUpdate from './favirote-update';
import FaviroteDeleteDialog from './favirote-delete-dialog';

const FaviroteRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Favirote />} />
    <Route path="new" element={<FaviroteUpdate />} />
    <Route path=":id">
      <Route index element={<FaviroteDetail />} />
      <Route path="edit" element={<FaviroteUpdate />} />
      <Route path="delete" element={<FaviroteDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default FaviroteRoutes;
