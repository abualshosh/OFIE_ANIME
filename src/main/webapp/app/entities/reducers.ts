import anime from 'app/entities/anime/anime.reducer';
import season from 'app/entities/season/season.reducer';
import source from 'app/entities/source/source.reducer';
import episode from 'app/entities/episode/episode.reducer';
import urlLink from 'app/entities/url-link/url-link.reducer';
import studio from 'app/entities/studio/studio.reducer';
import tag from 'app/entities/tag/tag.reducer';
import yearlySeason from 'app/entities/yearly-season/yearly-season.reducer';
import character from 'app/entities/character/character.reducer';
import comment from 'app/entities/comment/comment.reducer';
import favirote from 'app/entities/favirote/favirote.reducer';
import profile from 'app/entities/profile/profile.reducer';
import history from 'app/entities/history/history.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  anime,
  season,
  source,
  episode,
  urlLink,
  studio,
  tag,
  yearlySeason,
  character,
  comment,
  favirote,
  profile,
  history,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
