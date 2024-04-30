import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import UrlLink from './url-link';
import UrlLinkDetail from './url-link-detail';
import UrlLinkUpdate from './url-link-update';
import UrlLinkDeleteDialog from './url-link-delete-dialog';

const UrlLinkRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<UrlLink />} />
    <Route path="new" element={<UrlLinkUpdate />} />
    <Route path=":id">
      <Route index element={<UrlLinkDetail />} />
      <Route path="edit" element={<UrlLinkUpdate />} />
      <Route path="delete" element={<UrlLinkDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default UrlLinkRoutes;
