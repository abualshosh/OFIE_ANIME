import dayjs from 'dayjs';
import { IAnime } from 'app/shared/model/anime.model';
import { IProfile } from 'app/shared/model/profile.model';

export interface IFavirote {
  id?: number;
  addDate?: string | null;
  anime?: IAnime[] | null;
  profile?: IProfile | null;
}

export const defaultValue: Readonly<IFavirote> = {};
