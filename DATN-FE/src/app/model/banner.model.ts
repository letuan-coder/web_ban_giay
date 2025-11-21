export enum BannerType {
  HOMEPAGE_TOP = 'HOMEPAGE_TOP',
  HOMEPAGE_MIDDLE = 'HOMEPAGE_MIDDLE',
  HOMEPAGE_BOTTOM = 'HOMEPAGE_BOTTOM',
  MOBILE = 'MOBILE',
  DESKTOP = 'DESKTOP'
}

export interface Banner {
  id: string;
  imageUrl: string;
  bannerName: string;
  redirectUrl: string;
  sortOrder: number;
  active: boolean;
  startAt: string;
  endAt: string;
  type: BannerType;
}
