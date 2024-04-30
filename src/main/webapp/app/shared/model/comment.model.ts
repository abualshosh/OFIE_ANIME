import { IEpisode } from 'app/shared/model/episode.model';
import { IAnime } from 'app/shared/model/anime.model';
import { ISeason } from 'app/shared/model/season.model';
import { IProfile } from 'app/shared/model/profile.model';

export interface IComment {
  id?: number;
  comment?: string | null;
  like?: number | null;
  disLike?: number | null;
  episode?: IEpisode | null;
  anime?: IAnime | null;
  season?: ISeason | null;
  profile?: IProfile | null;
}

export const defaultValue: Readonly<IComment> = {};
