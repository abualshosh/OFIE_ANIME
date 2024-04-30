import dayjs from 'dayjs';
import { IUrlLink } from 'app/shared/model/url-link.model';
import { IHistory } from 'app/shared/model/history.model';
import { ISeason } from 'app/shared/model/season.model';
import { IComment } from 'app/shared/model/comment.model';

export interface IEpisode {
  id?: number;
  title?: string | null;
  episodeLink?: string | null;
  relaseDate?: string | null;
  urlLinks?: IUrlLink[] | null;
  history?: IHistory | null;
  season?: ISeason | null;
  comments?: IComment[] | null;
}

export const defaultValue: Readonly<IEpisode> = {};
