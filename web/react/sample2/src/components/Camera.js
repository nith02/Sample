import React from 'react';
import '../App.css';

class Camera extends React.Component {
    constructor(props) {
        super();
        this.state = {
            name: props.name,
            shop: props.shop,
        }

        this.handleEnterCamera = this.handleEnterCamera.bind(this);
    }

    handleEnterCamera(shop, camera) {
        window.location.hash = `#shop/${shop}/camera/${camera}`;
    }

    render() {
        return (
            <div className="Camera" onClick={() => this.handleEnterCamera(this.state.shop, this.state.name)}>
                {this.state.name}
            </div>
        )
    }
}

export default Camera;
