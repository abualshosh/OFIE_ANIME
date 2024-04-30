import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/anime">
        <Translate contentKey="global.menu.entities.anime" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/season">
        <Translate contentKey="global.menu.entities.season" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/source">
        <Translate contentKey="global.menu.entities.source" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/episode">
        <Translate contentKey="global.menu.entities.episode" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/url-link">
        <Translate contentKey="global.menu.entities.urlLink" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/studio">
        <Translate contentKey="global.menu.entities.studio" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/tag">
        <Translate contentKey="global.menu.entities.tag" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/yearly-season">
        <Translate contentKey="global.menu.entities.yearlySeason" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/character">
        <Translate contentKey="global.menu.entities.character" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/comment">
        <Translate contentKey="global.menu.entities.comment" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/favirote">
        <Translate contentKey="global.menu.entities.favirote" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/profile">
        <Translate contentKey="global.menu.entities.profile" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/history">
        <Translate contentKey="global.menu.entities.history" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
