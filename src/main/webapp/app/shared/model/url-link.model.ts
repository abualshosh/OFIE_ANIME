import { IEpisode } from 'app/shared/model/episode.model';
import { UrlLinkType } from 'app/shared/model/enumerations/url-link-type.model';

export interface IUrlLink {
  id?: number;
  linkType?: UrlLinkType | null;
  episode?: IEpisode | null;
}

export const defaultValue: Readonly<IUrlLink> = {};
