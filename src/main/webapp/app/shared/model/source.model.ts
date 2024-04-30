import { IAnime } from 'app/shared/model/anime.model';

export interface ISource {
  id?: number;
  name?: string | null;
  anime?: IAnime | null;
}

export const defaultValue: Readonly<ISource> = {};
