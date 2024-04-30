import { IAnime } from 'app/shared/model/anime.model';

export interface IStudio {
  id?: number;
  name?: string | null;
  anime?: IAnime[] | null;
}

export const defaultValue: Readonly<IStudio> = {};
