import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IUser } from 'app/shared/model/user.model';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { IFavirote } from 'app/shared/model/favirote.model';
import { getEntities as getFavirotes } from 'app/entities/favirote/favirote.reducer';
import { IComment } from 'app/shared/model/comment.model';
import { getEntities as getComments } from 'app/entities/comment/comment.reducer';
import { IHistory } from 'app/shared/model/history.model';
import { getEntities as getHistories } from 'app/entities/history/history.reducer';
import { IProfile } from 'app/shared/model/profile.model';
import { getEntity, updateEntity, createEntity, reset } from './profile.reducer';

export const ProfileUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const users = useAppSelector(state => state.userManagement.users);
  const favirotes = useAppSelector(state => state.favirote.entities);
  const comments = useAppSelector(state => state.comment.entities);
  const histories = useAppSelector(state => state.history.entities);
  const profileEntity = useAppSelector(state => state.profile.entity);
  const loading = useAppSelector(state => state.profile.loading);
  const updating = useAppSelector(state => state.profile.updating);
  const updateSuccess = useAppSelector(state => state.profile.updateSuccess);

  const handleClose = () => {
    navigate('/profile');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getUsers({}));
    dispatch(getFavirotes({}));
    dispatch(getComments({}));
    dispatch(getHistories({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...profileEntity,
      ...values,
      user: users.find(it => it.id.toString() === values.user.toString()),
      favirote: favirotes.find(it => it.id.toString() === values.favirote.toString()),
      comment: comments.find(it => it.id.toString() === values.comment.toString()),
      history: histories.find(it => it.id.toString() === values.history.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...profileEntity,
          user: profileEntity?.user?.id,
          favirote: profileEntity?.favirote?.id,
          comment: profileEntity?.comment?.id,
          history: profileEntity?.history?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.profile.home.createOrEditLabel" data-cy="ProfileCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.profile.home.createOrEditLabel">Create or edit a Profile</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="profile-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('ofieAnimeApp.profile.pictue')}
                id="profile-pictue"
                name="pictue"
                data-cy="pictue"
                type="text"
              />
              <ValidatedField id="profile-user" name="user" data-cy="user" label={translate('ofieAnimeApp.profile.user')} type="select">
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="profile-favirote"
                name="favirote"
                data-cy="favirote"
                label={translate('ofieAnimeApp.profile.favirote')}
                type="select"
              >
                <option value="" key="0" />
                {favirotes
                  ? favirotes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="profile-comment"
                name="comment"
                data-cy="comment"
                label={translate('ofieAnimeApp.profile.comment')}
                type="select"
              >
                <option value="" key="0" />
                {comments
                  ? comments.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="profile-history"
                name="history"
                data-cy="history"
                label={translate('ofieAnimeApp.profile.history')}
                type="select"
              >
                <option value="" key="0" />
                {histories
                  ? histories.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/profile" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ProfileUpdate;
