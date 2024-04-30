import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './season.reducer';

export const SeasonDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const seasonEntity = useAppSelector(state => state.season.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="seasonDetailsHeading">
          <Translate contentKey="ofieAnimeApp.season.detail.title">Season</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.id}</dd>
          <dt>
            <span id="titleInJapan">
              <Translate contentKey="ofieAnimeApp.season.titleInJapan">Title In Japan</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.titleInJapan}</dd>
          <dt>
            <span id="titleInEnglis">
              <Translate contentKey="ofieAnimeApp.season.titleInEnglis">Title In Englis</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.titleInEnglis}</dd>
          <dt>
            <span id="relaseDate">
              <Translate contentKey="ofieAnimeApp.season.relaseDate">Relase Date</Translate>
            </span>
          </dt>
          <dd>
            {seasonEntity.relaseDate ? <TextFormat value={seasonEntity.relaseDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="addDate">
              <Translate contentKey="ofieAnimeApp.season.addDate">Add Date</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.addDate ? <TextFormat value={seasonEntity.addDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="startDate">
              <Translate contentKey="ofieAnimeApp.season.startDate">Start Date</Translate>
            </span>
          </dt>
          <dd>
            {seasonEntity.startDate ? <TextFormat value={seasonEntity.startDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="endDate">
              <Translate contentKey="ofieAnimeApp.season.endDate">End Date</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.endDate ? <TextFormat value={seasonEntity.endDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="avrgeEpisodeLength">
              <Translate contentKey="ofieAnimeApp.season.avrgeEpisodeLength">Avrge Episode Length</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.avrgeEpisodeLength}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="ofieAnimeApp.season.type">Type</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.type}</dd>
          <dt>
            <span id="seasonType">
              <Translate contentKey="ofieAnimeApp.season.seasonType">Season Type</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.seasonType}</dd>
          <dt>
            <span id="cover">
              <Translate contentKey="ofieAnimeApp.season.cover">Cover</Translate>
            </span>
          </dt>
          <dd>{seasonEntity.cover}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.season.anime">Anime</Translate>
          </dt>
          <dd>{seasonEntity.anime ? seasonEntity.anime.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.season.yearlySeason">Yearly Season</Translate>
          </dt>
          <dd>{seasonEntity.yearlySeason ? seasonEntity.yearlySeason.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/season" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/season/${seasonEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SeasonDetail;
