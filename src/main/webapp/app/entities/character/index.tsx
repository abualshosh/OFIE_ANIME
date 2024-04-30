import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Character from './character';
import CharacterDetail from './character-detail';
import CharacterUpdate from './character-update';
import CharacterDeleteDialog from './character-delete-dialog';

const CharacterRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Character />} />
    <Route path="new" element={<CharacterUpdate />} />
    <Route path=":id">
      <Route index element={<CharacterDetail />} />
      <Route path="edit" element={<CharacterUpdate />} />
      <Route path="delete" element={<CharacterDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default CharacterRoutes;
