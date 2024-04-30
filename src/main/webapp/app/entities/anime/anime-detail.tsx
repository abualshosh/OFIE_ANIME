import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, byteSize, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './anime.reducer';

export const AnimeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const animeEntity = useAppSelector(state => state.anime.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="animeDetailsHeading">
          <Translate contentKey="ofieAnimeApp.anime.detail.title">Anime</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{animeEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="ofieAnimeApp.anime.title">Title</Translate>
            </span>
          </dt>
          <dd>{animeEntity.title}</dd>
          <dt>
            <span id="discription">
              <Translate contentKey="ofieAnimeApp.anime.discription">Discription</Translate>
            </span>
          </dt>
          <dd>{animeEntity.discription}</dd>
          <dt>
            <span id="cover">
              <Translate contentKey="ofieAnimeApp.anime.cover">Cover</Translate>
            </span>
          </dt>
          <dd>{animeEntity.cover}</dd>
          <dt>
            <span id="relaseDate">
              <Translate contentKey="ofieAnimeApp.anime.relaseDate">Relase Date</Translate>
            </span>
          </dt>
          <dd>
            {animeEntity.relaseDate ? <TextFormat value={animeEntity.relaseDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.anime.source">Source</Translate>
          </dt>
          <dd>{animeEntity.source ? animeEntity.source.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.anime.studio">Studio</Translate>
          </dt>
          <dd>{animeEntity.studio ? animeEntity.studio.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.anime.favirote">Favirote</Translate>
          </dt>
          <dd>{animeEntity.favirote ? animeEntity.favirote.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/anime" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/anime/${animeEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AnimeDetail;
