import dayjs from 'dayjs';
import { ISeason } from 'app/shared/model/season.model';

export interface IYearlySeason {
  id?: number;
  name?: string | null;
  seasons?: ISeason[] | null;
}

export const defaultValue: Readonly<IYearlySeason> = {};
