import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Anime from './anime';
import Season from './season';
import Source from './source';
import Episode from './episode';
import UrlLink from './url-link';
import Studio from './studio';
import Tag from './tag';
import YearlySeason from './yearly-season';
import Character from './character';
import Comment from './comment';
import Favirote from './favirote';
import Profile from './profile';
import History from './history';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="anime/*" element={<Anime />} />
        <Route path="season/*" element={<Season />} />
        <Route path="source/*" element={<Source />} />
        <Route path="episode/*" element={<Episode />} />
        <Route path="url-link/*" element={<UrlLink />} />
        <Route path="studio/*" element={<Studio />} />
        <Route path="tag/*" element={<Tag />} />
        <Route path="yearly-season/*" element={<YearlySeason />} />
        <Route path="character/*" element={<Character />} />
        <Route path="comment/*" element={<Comment />} />
        <Route path="favirote/*" element={<Favirote />} />
        <Route path="profile/*" element={<Profile />} />
        <Route path="history/*" element={<History />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
