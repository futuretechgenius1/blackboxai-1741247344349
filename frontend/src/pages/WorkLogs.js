import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { toast } from 'react-toastify';
import {
  PlusIcon,
  PencilIcon,
  TrashIcon,
  CheckIcon,
  XMarkIcon,
} from '@heroicons/react/24/outline';

const WorkLogs = () => {
  const { user } = useAuth();
  const [workLogs, setWorkLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingLog, setEditingLog] = useState(null);
  const [formData, setFormData] = useState({
    date: '',
    hoursWorked: '',
    remarks: '',
  });

  useEffect(() => {
    fetchWorkLogs();
  }, []);

  const fetchWorkLogs = async () => {
    try {
      const response = await axios.get('/api/worklogs');
      setWorkLogs(response.data);
    } catch (error) {
      toast.error('Failed to fetch work logs');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingLog) {
        await axios.put(`/api/worklogs/${editingLog.id}`, formData);
        toast.success('Work log updated successfully');
      } else {
        await axios.post('/api/worklogs', formData);
        toast.success('Work log created successfully');
      }
      resetForm();
      fetchWorkLogs();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleEdit = (log) => {
    setEditingLog(log);
    setFormData({
      date: log.date,
      hoursWorked: log.hoursWorked,
      remarks: log.remarks,
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this work log?')) {
      return;
    }

    try {
      await axios.delete(`/api/worklogs/${id}`);
      toast.success('Work log deleted successfully');
      fetchWorkLogs();
    } catch (error) {
      toast.error('Failed to delete work log');
    }
  };

  const resetForm = () => {
    setFormData({ date: '', hoursWorked: '', remarks: '' });
    setEditingLog(null);
    setShowForm(false);
  };

  const handleApprove = async (id) => {
    try {
      await axios.put(`/api/worklogs/${id}/approve`);
      toast.success('Work log approved');
      fetchWorkLogs();
    } catch (error) {
      toast.error('Failed to approve work log');
    }
  };

  const handleReject = async (id) => {
    try {
      await axios.put(`/api/worklogs/${id}/reject`);
      toast.success('Work log rejected');
      fetchWorkLogs();
    } catch (error) {
      toast.error('Failed to reject work log');
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="px-4 sm:px-6 lg:px-8">
      <div className="sm:flex sm:items-center">
        <div className="sm:flex-auto">
          <h1 className="text-2xl font-semibold text-gray-900">Work Logs</h1>
          <p className="mt-2 text-sm text-gray-700">
            Manage your work logs and track your hours.
          </p>
        </div>
        <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
          <button
            onClick={() => setShowForm(!showForm)}
            className="inline-flex items-center justify-center rounded-md border border-transparent bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700"
          >
            <PlusIcon className="-ml-1 mr-2 h-5 w-5" />
            New Work Log
          </button>
        </div>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="mt-8 space-y-6 bg-white p-6 rounded-lg shadow">
          <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
            <div className="sm:col-span-2">
              <label htmlFor="date" className="block text-sm font-medium text-gray-700">
                Date
              </label>
              <input
                type="date"
                name="date"
                id="date"
                required
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
              />
            </div>

            <div className="sm:col-span-2">
              <label htmlFor="hoursWorked" className="block text-sm font-medium text-gray-700">
                Hours Worked
              </label>
              <input
                type="number"
                name="hoursWorked"
                id="hoursWorked"
                required
                min="0"
                max="24"
                step="0.5"
                value={formData.hoursWorked}
                onChange={(e) => setFormData({ ...formData, hoursWorked: e.target.value })}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
              />
            </div>

            <div className="sm:col-span-6">
              <label htmlFor="remarks" className="block text-sm font-medium text-gray-700">
                Remarks
              </label>
              <textarea
                id="remarks"
                name="remarks"
                rows={3}
                value={formData.remarks}
                onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm"
              />
            </div>
          </div>

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={resetForm}
              className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="rounded-md border border-transparent bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700"
            >
              {editingLog ? 'Update' : 'Create'} Work Log
            </button>
          </div>
        </form>
      )}

      <div className="mt-8 flex flex-col">
        <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
          <div className="inline-block min-w-full py-2 align-middle">
            <div className="overflow-hidden shadow-sm ring-1 ring-black ring-opacity-5">
              <table className="min-w-full divide-y divide-gray-300">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Date</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Hours</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Remarks</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold text-gray-900">Status</th>
                    <th className="px-6 py-3 text-right text-sm font-semibold text-gray-900">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {workLogs.map((log) => (
                    <tr key={log.id}>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                        {new Date(log.date).toLocaleDateString()}
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-900">
                        {log.hoursWorked}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">{log.remarks}</td>
                      <td className="whitespace-nowrap px-6 py-4 text-sm">
                        <span
                          className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${
                            log.status === 'APPROVED'
                              ? 'bg-green-100 text-green-800'
                              : log.status === 'REJECTED'
                              ? 'bg-red-100 text-red-800'
                              : 'bg-yellow-100 text-yellow-800'
                          }`}
                        >
                          {log.status}
                        </span>
                      </td>
                      <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                        <div className="flex justify-end space-x-3">
                          {user.isAdmin && log.status === 'PENDING' && (
                            <>
                              <button
                                onClick={() => handleApprove(log.id)}
                                className="text-green-600 hover:text-green-900"
                              >
                                <CheckIcon className="h-5 w-5" />
                              </button>
                              <button
                                onClick={() => handleReject(log.id)}
                                className="text-red-600 hover:text-red-900"
                              >
                                <XMarkIcon className="h-5 w-5" />
                              </button>
                            </>
                          )}
                          {log.status === 'PENDING' && (
                            <>
                              <button
                                onClick={() => handleEdit(log)}
                                className="text-primary-600 hover:text-primary-900"
                              >
                                <PencilIcon className="h-5 w-5" />
                              </button>
                              <button
                                onClick={() => handleDelete(log.id)}
                                className="text-red-600 hover:text-red-900"
                              >
                                <TrashIcon className="h-5 w-5" />
                              </button>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WorkLogs;
