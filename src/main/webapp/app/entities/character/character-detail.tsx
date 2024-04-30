import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './character.reducer';

export const CharacterDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const characterEntity = useAppSelector(state => state.character.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="characterDetailsHeading">
          <Translate contentKey="ofieAnimeApp.character.detail.title">Character</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{characterEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="ofieAnimeApp.character.name">Name</Translate>
            </span>
          </dt>
          <dd>{characterEntity.name}</dd>
          <dt>
            <span id="picture">
              <Translate contentKey="ofieAnimeApp.character.picture">Picture</Translate>
            </span>
          </dt>
          <dd>{characterEntity.picture}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.character.anime">Anime</Translate>
          </dt>
          <dd>{characterEntity.anime ? characterEntity.anime.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/character" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/character/${characterEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CharacterDetail;
