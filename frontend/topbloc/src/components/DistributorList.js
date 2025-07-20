import React, { useState, useEffect } from 'react';
import axios from 'axios';
import AddDistributorItem from './AddDistributorItem';

function DistributorList() {
  const [distributors, setDistributors] = useState([]);
  const [selectedDistributor, setSelectedDistributor] = useState(null);
  const [distributorItems, setDistributorItems] = useState([]);

  useEffect(() => {
    fetchDistributors();
  }, []);

  useEffect(() => {
    if (selectedDistributor) {
      fetchDistributorItems(selectedDistributor.id);
    }
  }, [selectedDistributor]);

  const fetchDistributors = async () => {
    try {
      const response = await axios.get('http://localhost:4567/distributors');
      setDistributors(response.data);
    } catch (error) {
      console.error('Error fetching distributors:', error);
    }
  };

  const fetchDistributorItems = async (id) => {
    try {
      const response = await axios.get(`http://localhost:4567/distributor/${id}`);
      setDistributorItems(response.data);
    } catch (error) {
      console.error('Error fetching distributor items:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await axios.delete(`http://localhost:4567/distributor/${id}`);
      fetchDistributors();
      if (selectedDistributor && selectedDistributor.id === id) {
        setSelectedDistributor(null);
        setDistributorItems([]);
      }
    } catch (error) {
      console.error('Error deleting distributor:', error);
    }
  };

  return (
    <div>
      <AddDistributorItem />
      
      <div className="distributor-container">
        <div className="distributor-list">
          <h3>Distributors</h3>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {distributors.map(dist => (
                <tr key={dist.id} onClick={() => setSelectedDistributor(dist)}>
                  <td>{dist.id}</td>
                  <td>{dist.name}</td>
                  <td>
                    <button onClick={(e) => { e.stopPropagation(); handleDelete(dist.id); }}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        
        {selectedDistributor && (
          <div className="distributor-items">
            <h3>Items from {selectedDistributor.name}</h3>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>ID</th>
                  <th>Cost</th>
                </tr>
              </thead>
              <tbody>
                {distributorItems.map(item => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.id}</td>
                    <td>${item.cost?.toFixed(2) || "N/A"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

export default DistributorList;