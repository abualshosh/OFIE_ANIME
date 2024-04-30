import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Anime from './anime';
import AnimeDetail from './anime-detail';
import AnimeUpdate from './anime-update';
import AnimeDeleteDialog from './anime-delete-dialog';

const AnimeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Anime />} />
    <Route path="new" element={<AnimeUpdate />} />
    <Route path=":id">
      <Route index element={<AnimeDetail />} />
      <Route path="edit" element={<AnimeUpdate />} />
      <Route path="delete" element={<AnimeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AnimeRoutes;
