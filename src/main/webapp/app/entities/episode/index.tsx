import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Episode from './episode';
import EpisodeDetail from './episode-detail';
import EpisodeUpdate from './episode-update';
import EpisodeDeleteDialog from './episode-delete-dialog';

const EpisodeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Episode />} />
    <Route path="new" element={<EpisodeUpdate />} />
    <Route path=":id">
      <Route index element={<EpisodeDetail />} />
      <Route path="edit" element={<EpisodeUpdate />} />
      <Route path="delete" element={<EpisodeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EpisodeRoutes;
