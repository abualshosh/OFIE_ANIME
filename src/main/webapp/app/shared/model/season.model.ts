import dayjs from 'dayjs';
import { IEpisode } from 'app/shared/model/episode.model';
import { IAnime } from 'app/shared/model/anime.model';
import { IYearlySeason } from 'app/shared/model/yearly-season.model';
import { IComment } from 'app/shared/model/comment.model';
import { Type } from 'app/shared/model/enumerations/type.model';
import { SeasonType } from 'app/shared/model/enumerations/season-type.model';

export interface ISeason {
  id?: number;
  titleInJapan?: string | null;
  titleInEnglis?: string | null;
  relaseDate?: string | null;
  addDate?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  avrgeEpisodeLength?: string | null;
  type?: Type | null;
  seasonType?: SeasonType | null;
  cover?: string | null;
  episodes?: IEpisode[] | null;
  anime?: IAnime | null;
  yearlySeason?: IYearlySeason | null;
  comments?: IComment[] | null;
}

export const defaultValue: Readonly<ISeason> = {};
