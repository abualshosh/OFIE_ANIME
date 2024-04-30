import dayjs from 'dayjs';
import { IProfile } from 'app/shared/model/profile.model';
import { IEpisode } from 'app/shared/model/episode.model';

export interface IHistory {
  id?: number;
  date?: string | null;
  profile?: IProfile | null;
  episodes?: IEpisode[] | null;
}

export const defaultValue: Readonly<IHistory> = {};
