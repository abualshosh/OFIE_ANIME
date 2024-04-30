import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, byteSize, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './episode.reducer';

export const EpisodeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const episodeEntity = useAppSelector(state => state.episode.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="episodeDetailsHeading">
          <Translate contentKey="ofieAnimeApp.episode.detail.title">Episode</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{episodeEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="ofieAnimeApp.episode.title">Title</Translate>
            </span>
          </dt>
          <dd>{episodeEntity.title}</dd>
          <dt>
            <span id="episodeLink">
              <Translate contentKey="ofieAnimeApp.episode.episodeLink">Episode Link</Translate>
            </span>
          </dt>
          <dd>{episodeEntity.episodeLink}</dd>
          <dt>
            <span id="relaseDate">
              <Translate contentKey="ofieAnimeApp.episode.relaseDate">Relase Date</Translate>
            </span>
          </dt>
          <dd>
            {episodeEntity.relaseDate ? <TextFormat value={episodeEntity.relaseDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.episode.history">History</Translate>
          </dt>
          <dd>{episodeEntity.history ? episodeEntity.history.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.episode.season">Season</Translate>
          </dt>
          <dd>{episodeEntity.season ? episodeEntity.season.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/episode" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/episode/${episodeEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default EpisodeDetail;
