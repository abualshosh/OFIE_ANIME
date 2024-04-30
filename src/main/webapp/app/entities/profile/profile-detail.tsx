import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './profile.reducer';

export const ProfileDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const profileEntity = useAppSelector(state => state.profile.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="profileDetailsHeading">
          <Translate contentKey="ofieAnimeApp.profile.detail.title">Profile</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{profileEntity.id}</dd>
          <dt>
            <span id="pictue">
              <Translate contentKey="ofieAnimeApp.profile.pictue">Pictue</Translate>
            </span>
          </dt>
          <dd>{profileEntity.pictue}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.profile.user">User</Translate>
          </dt>
          <dd>{profileEntity.user ? profileEntity.user.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.profile.favirote">Favirote</Translate>
          </dt>
          <dd>{profileEntity.favirote ? profileEntity.favirote.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.profile.comment">Comment</Translate>
          </dt>
          <dd>{profileEntity.comment ? profileEntity.comment.id : ''}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.profile.history">History</Translate>
          </dt>
          <dd>{profileEntity.history ? profileEntity.history.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/profile" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/profile/${profileEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default ProfileDetail;
